const Chat = require('../models/Chat');
const User = require('../models/User');


const removeChat = async (userId, chatId) => {
    try {
        // Remove the chat from the database
        await Chat.findByIdAndDelete(chatId);

        // Fetch the user and their friend who were participating in the chat
        const user = await User.findById(userId);
        const chat = await Chat.findById(chatId);
        const friendUserFromChat = chat.users.find(user => user.userId.toString() !== userId);
        const friendUser = await User.findById(friendUserFromChat.userId);

        // Emit updated chat list to the user
        const userChats = await Chat.find({ 'users.userId': user._id });
        const userChatList = getUserChatList(userChats, userId);
        io.to(user.socketId).emit('chats', userChatList);

        // Emit updated chat list to the friend
        const friendChats = await Chat.find({ 'users.userId': friendUser._id });
        const friendChatList = getUserChatList(friendChats, friendUser._id);
        io.to(friendUser.socketId).emit('chats', friendChatList);
    } catch (error) {
        console.error('Error removing chat:', error);
    }
};

// Utility function to generate chat list for a user
const getUserChatList = async (chats, userId) => {







   
    
    
    const chatList = [];
    for (const chat of chats) {
      const participants = await User.find({ _id: { $in: chat.users.map(u => u.userId) } });
      console.log(`in removeChat and participants of ${chat._id}is ${participants}`)
      const otherParticipant = participants.find(participant => participant._id.toString() !== userId.toString());
      console.log(`in removeChat and otherParticipant of ${userId}is ${otherParticipant}`)
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
    console.log(`chatList: ${chatList[0]}`)
    return chatList;
};

module.exports = { getUserChatList};