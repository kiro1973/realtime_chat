
const Chat = require('../models/Chat');
const { messageSchema, Message } = require('../models/Message');
const User = require('../models/User');
const { updateUserUnreadMessageStatus } = require('../models/queries');

const socketIO = require('socket.io');
async function sendMessage(userId, chatId,message,socket) {
try {
    const user = await User.findById(userId);
    if (!user) {
      console.log('in send Message and userId is wrong');
      return;
    }
    let chat = await Chat.findById(chatId);
    console.log(`in sendMessage and chat: ${chat}`)
    const friendUserFromChat= chat.users.find(user => user.userId.toString() !== userId);
    const friendUser=await User.findById(friendUserFromChat.userId);
    if (!chat) {
      console.log('trying to send message with wrong or not fount chatId');
      return;
    }
    const newMessage = new Message({
      message,
      from: user._id,
      timestamp: Date.now(),
    });
    const newMessageToSend = {
      message,
      from: user.gName,
      timestamp: newMessage.timestamp,
    };
    chat.messages.push(newMessage);
    await updateUserUnreadMessageStatus(friendUser._id,chatId)
    await chat.save();
    await newMessage.save();
    socket.broadcast.to(friendUser.socketId).emit('newMessage', newMessageToSend);
    const chats = await Chat.find({ 'users.userId': friendUser._id });
    const chatList = [];
    for (const chat of chats) {
      const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
      const otherParticipant = participants.find(participant => participant._id.toString() !== friendUser._id.toString());  
      const lastMessage = chat.messages.length > 0 ? chat.messages[chat.messages.length - 1] : null;
      const userToSend=chat.users.find(u => u.userId.toString() == friendUser._id.toString());
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
    socket.broadcast.to(friendUser.socketId).emit('chats', chatList);
    const messages = chat.messages || [];
    socket.broadcast.to(friendUser.socketId).emit('chatHistory', messages);
    socket.emit('chatHistory', messages);

  } catch (error) {
    console.error('Error sending message:', error);
  }
}
module.exports = { sendMessage };
