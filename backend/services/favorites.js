// Import the required models
const Chat = require('../models/Chat');

// Define the service function
async function addChatToFavorites(userId, chatId) {
  try {
    // Find the chat by its ID
    const chat = await Chat.findById(chatId);

    // Check if the chat exists
    if (!chat) {
      console.error('Chat not found');
      return;
    }

    // Find the user's object in the chat
    const userInChat = chat.users.find(user => user.userId.toString() === userId);

    // Check if the user exists in the chat
    if (!userInChat) {
      console.error('User not found in the chat');
      return;
    }

    // Set the isChatFavorite flag to true for the user
    userInChat.isChatFavorite = true;

    // Save the updated chat
    await chat.save();

    console.log('Chat added to favorites successfully');
  } catch (error) {
    console.error('Error adding chat to favorites:', error);
  }
}



// Define the service function
async function removeChatFromFavorites(userId, chatId) {
  try {
    // Find the chat by its ID
    const chat = await Chat.findById(chatId);

    // Check if the chat exists
    if (!chat) {
      console.error('Chat not found');
      return;
    }

    // Find the user's object in the chat
    const userInChat = chat.users.find(user => user.userId.toString() === userId);

    // Check if the user exists in the chat
    if (!userInChat) {
      console.error('User not found in the chat');
      return;
    }

    // Set the isChatFavorite flag to true for the user
    userInChat.isChatFavorite = false;

    // Save the updated chat
    await chat.save();

    console.log('Chat removed from favorites successfully');
  } catch (error) {
    console.error('Error removing chat from favorites:', error);
  }
}




module.exports = { addChatToFavorites,removeChatFromFavorites };
