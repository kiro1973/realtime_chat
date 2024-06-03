const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const mongoose = require('mongoose');

const app = express();
const server = http.createServer(app);
const io = socketIO(server);

mongoose.connect('mongodb://127.0.0.1:27017/chatApp', {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

// Import models
const { messageSchema, Message } = require('./models/Message');
const Chat = require('./models/Chat');
const User = require('./models/User');
const { updateUserUnreadMessageStatus } = require('./models/queries');
const { addChatToFavorites,removeChatFromFavorites } = require('./services/favorites');
const { markChatAsRead } = require('./services/unreads');
const { getUserChatList } = require('./services/removeChat');
const { sendMessage } = require('./services/sending');
const { deleteMessage } = require('./services/delete');

io.on('connection', (socket) => {
  console.log('User connected:', socket.id);

  // Other socket event listeners...

  socket.on('saveSocketId', async (userId) => {
    console.log('in saveSocketId')
    try {
      if (typeof(userId)!= 'string')
        return;
      const user = await User.findById(userId);
      console.log(`user that you want to save socket id is ${socket.id}`)
      if (user) {
        user.socketId = socket.id;
        await user.save();
        console.log('user that you want to save socket id is found\n////////')
      }
    } catch (error) {
      console.error('Error updating user socketId:', error);
    }
  });

  socket.on('createChat', async ( userId, gName ) => {
    console.log(`In createChat and received user with userId: ${userId}`)
    console.log(`In createChat and received user with gName: ${gName }`)
    
    try {
      const user = await User.findById(userId);

      if (!user) {
        console.error('User not found');
        return;
      }

      const friend = await User.findOne({ gName });

      if (!friend) {
        console.error('Friend not found');
        return;
      }
      console.log(`In createChat and received user with user._id: ${user._id}`)
      console.log(`In createChat and received user with friend._id: ${friend._id}`)
      
      const previousChat= await Chat.findOne({
        users: {
          $all: [
            {"$elemMatch": {
              userId: user._id
            }},
            {"$elemMatch": {
              userId: friend._id
            }},
          ],
        },
      });
      console.log(`previousChat: ${previousChat}`)
      if (previousChat==null){
        const newChat = new Chat({
          users: [
            { userId: user._id, isChatFavorite: false,isThereUnReadMessage: false },
            { userId: friend._id, isChatFavorite: false,isThereUnReadMessage: false },
          ],
          messages: [],
          
        });
        await newChat.save();

        //we have saved the chat and now we send to the friend added the his chats after added


        const chats = await Chat.find({ 'users.userId': friend._id });
        const chatList = [];
        for (const chat of chats) {
          const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
          const otherParticipant = participants.find(participant => participant._id.toString() !== friend._id.toString());  
          const lastMessage = chat.messages.length > 0 ? chat.messages[chat.messages.length - 1] : null;
          const userToSend=chat.users.find(u => u.userId.toString() == friend._id.toString());
          const isChatFavorite = userToSend?.isChatFavorite || false;
          //const isThereUnreadMessages = chat.isThereUnReadMessage || false;
          const isThereUnreadMessages = userToSend.isThereUnReadMessage || false;
  
          const lastMessageSender = lastMessage ? lastMessage.from : null;
  
          chatList.push({
            chatId: chat._id,
            friendName: otherParticipant.gName,
            friendImage: otherParticipant.gImage,
            lastMessage: chat.messages.length > 0 ? chat.messages[chat.messages.length - 1].message : '',
            lastMessageTimeStamp: chat.messages.length > 0 ? chat.messages[chat.messages.length - 1].timestamp : '',
            isChatFavorite,
            isThereUnreadMessages,
            //lastMessageSender,
          });
        }
        socket.broadcast.to(friend.socketId).emit('chats', chatList);

        //finished the part of sending the added user his new ChatList
        console.log(`new chat created`)
      }
    } catch (error) {
      console.error('Error creating chat:', error);
    }
  });

  socket.on('disconnect', async () => {
    console.log('User disconnected:', socket.id);
    try {
      const user = await User.findOne({ socketId: socket.id });
      if (user) {
        user.socketId = null;
        await user.save();
      }
    } catch (error) {
      console.error('Error updating user socketId:', error);
    }
  });

  socket.on('getChats', async (userId) => {
    console.log(` someone wants "getChats" with userId:${userId}`)
    try {
      const user = await User.findById(userId);
      if (!user) {
        return;
      }
      const chats = await Chat.find({ 'users.userId': user._id });
      const chatList = [];
      for (const chat of chats) {
        const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
        const otherParticipant = participants.find(participant => participant._id.toString() !== userId);
        const lastMessage = chat.messages.length > 0 ? chat.messages[chat.messages.length - 1] : null;
        const currentUserObjectInChat = chat.users.find(u => u.userId.toString() == userId);
        const isChatFavorite =currentUserObjectInChat?.isChatFavorite || false;
        const isThereUnreadMessages = currentUserObjectInChat?.isThereUnReadMessage || false;
        const lastMessageSender = lastMessage ? lastMessage.from : null;

        chatList.push({
          chatId: chat._id,
          friendName: otherParticipant.gName,
          friendImage: otherParticipant.gImage,
          lastMessage: lastMessage ? lastMessage.message : '',
          lastMessageTimeStamp: lastMessage ? lastMessage.timestamp : '',
          isChatFavorite,
          isThereUnreadMessages,
          //lastMessageSender,
        });
      }

      // Sort the chat list
      chatList.sort((a, b) => {
        // Sort by favorite status (favorites first)
        if (a.isChatFavorite !== b.isChatFavorite) {
          return b.isChatFavorite - a.isChatFavorite;
        }
        // Sort by unread messages (unread first)
        if (a.isThereUnreadMessages !== b.isThereUnreadMessages) {
          return b.isThereUnreadMessages - a.isThereUnreadMessages;
        }
        // Sort by last message timestamp (latest first)
        return new Date(b.lastMessageTimeStamp) - new Date(a.lastMessageTimeStamp);
      });

      socket.emit('chats', chatList);
    } catch (error) {
      console.error('Error fetching chats:', error);
    }
  });

  socket.on('getChatHistory', async (chatId) => {
    const chat = await Chat.findById(chatId);
    const messages = chat.messages || [];
    socket.emit('chatHistory',  messages);
  });

  socket.on('sendMessage', async ({ chatId, userId, message }) => {
    sendMessage(userId,chatId,message,socket)
  });
  socket.on('deleteMessage', async ({ messageId, userId }) => {
    deleteMessage(messageId,userId,socket)
  });

  socket.on('markAsRead', async ({ chatId }) => {
    try {
      const chat = await Chat.findById(chatId);
      if (chat) {
        chat.isThereUnReadMessage=false;
        await chat.save();
      }
    } catch (error) {
      console.error('Error marking message as read:', error);
    }
  });


// Inside your socket event listener or wherever you need to add a chat to favorites
socket.on('addChatToFavorites', async (userId, chatId) => {
  await addChatToFavorites(userId, chatId);
});
socket.on('removeChatFromFavorites', async (userId, chatId) => {
  await removeChatFromFavorites(userId, chatId);
});

socket.on('markChatAsRead', async (userId, chatId) => {
    await markChatAsRead(userId, chatId);
  });

  socket.on('removeChat', async ({ userId, chatId }) => {
  //   try {
  //     // Remove the chat from the database
      

  //     // Fetch the user and their friend who were participating in the chat
  //     const user = await User.findById(userId);
  //     const chat = await Chat.findById(chatId);
  //     console.log(`chat ${chat}`)
  //     const friendUserFromChat = chat.users.find(user => user.userId.toString() !== userId);
  //     console.log(`friendUserFromChat ${friendUserFromChat}`)
  //     const friendUser = await User.findById(friendUserFromChat.userId);
  //     console.log(`friendUser: ${friendUser}`)

  //     // Emit updated chat list to the user
  //     const userChats = await Chat.find({ 'users.userId': user._id });
  //     console.log(` ${userChats}`)
  //     const userChatList = getUserChatList(userChats, userId);
      
      

  //     // Emit updated chat list to the friend
  //     const friendChats = await Chat.find({ 'users.userId': friendUser._id });
  //     const friendChatList = getUserChatList(friendChats, friendUser._id);
  //     socket.emit('chats',     userChatList);
  //     socket.broadcast.to(friendUser.socketId).emit('chats', friendChatList);
  //     await Chat.findByIdAndDelete(chatId);
  // } catch (error) {
  //     console.error('Error removing chat:', error);
  // }










try{


  const user = await User.findById(userId);
  const thisChat = await Chat.findById(chatId);//I have got te chat json I must delete chat now to avoid reading it as a current chat
  await Chat.findByIdAndDelete(chatId);
  const friendUserFromChat= thisChat.users.find(user => user.userId.toString() !== userId);
  const friendUser=await User.findById(friendUserFromChat.userId);

  const partnerChats = await Chat.find({ 'users.userId': friendUser._id });

  const partnerChatList = [];
  for (const chat of partnerChats) {
    const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
    const otherParticipant = participants.find(participant => participant._id.toString() !== friendUser._id.toString());  
    const lastMessage = chat.messages.length > 0 ? chat.messages[chat.messages.length - 1] : null;
    const userToSend=chat.users.find(u => u.userId.toString() == friendUser._id.toString());
    const isChatFavorite = userToSend?.isChatFavorite || false;
    //const isThereUnreadMessages = chat.isThereUnReadMessage || false;
    const isThereUnreadMessages = userToSend.isThereUnReadMessage || false;

    const lastMessageSender = lastMessage ? lastMessage.from : null;

    partnerChatList.push({
      chatId: chat._id,
      friendName: otherParticipant.gName,
      friendImage: otherParticipant.gImage,
      lastMessage: chat.messages.length > 0 ? chat.messages[chat.messages.length - 1].message : '',
      lastMessageTimeStamp: chat.messages.length > 0 ? chat.messages[chat.messages.length - 1].timestamp : '',
      isChatFavorite,
      isThereUnreadMessages,
      //lastMessageSender,
    });



  }

  partnerChatList.sort((a, b) => {
    // Sort by favorite status (favorites first)
    if (a.isChatFavorite !== b.isChatFavorite) {
      return b.isChatFavorite - a.isChatFavorite;
    }
    // Sort by unread messages (unread first)
    if (a.isThereUnreadMessages !== b.isThereUnreadMessages) {
      return b.isThereUnreadMessages - a.isThereUnreadMessages;
    }
    // Sort by last message timestamp (latest first)
    return new Date(b.lastMessageTimeStamp) - new Date(a.lastMessageTimeStamp);
  });
  socket.broadcast.to(friendUser.socketId).emit('chats', partnerChatList);







  const chats = await Chat.find({ 'users.userId': user._id });
  const chatList = [];
  for (const chat of chats) {
    const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
    const otherParticipant = participants.find(participant => participant._id.toString() !== userId);
    const lastMessage = chat.messages.length > 0 ? chat.messages[chat.messages.length - 1] : null;
    const currentUserObjectInChat = chat.users.find(u => u.userId.toString() == userId);
    const isChatFavorite =currentUserObjectInChat?.isChatFavorite || false;
    const isThereUnreadMessages = currentUserObjectInChat?.isThereUnReadMessage || false;
    const lastMessageSender = lastMessage ? lastMessage.from : null;

    chatList.push({
      chatId: chat._id,
      friendName: otherParticipant.gName,
      friendImage: otherParticipant.gImage,
      lastMessage: lastMessage ? lastMessage.message : '',
      lastMessageTimeStamp: lastMessage ? lastMessage.timestamp : '',
      isChatFavorite,
      isThereUnreadMessages,
      //lastMessageSender,
    });
  }

  // Sort the chat list
  chatList.sort((a, b) => {
    // Sort by favorite status (favorites first)
    if (a.isChatFavorite !== b.isChatFavorite) {
      return b.isChatFavorite - a.isChatFavorite;
    }
    // Sort by unread messages (unread first)
    if (a.isThereUnreadMessages !== b.isThereUnreadMessages) {
      return b.isThereUnreadMessages - a.isThereUnreadMessages;
    }
    // Sort by last message timestamp (latest first)
    return new Date(b.lastMessageTimeStamp) - new Date(a.lastMessageTimeStamp);
  });

  socket.emit('chats', chatList);





  } catch (error) {
      console.error('Error removing chat:', error);
  }




});




});

// Express GET method to fetch chat history
app.get('/chatHistory/:chatId', async (req, res) => {
  try {
    const chatId = req.params.chatId;
    const chat = await Chat.findById(chatId).populate('messages');
    if (chat){
    console.log(`in Get/chatHistoryfound chat with the required chat Id \n ${chat.messages} \n ////////////////////`)
    const messages = chat.messages || [];
    res.json({ chatId, messages });
    }
    
  } catch (error) {
    console.error('Error fetching chat history:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
