// Import the required models
const Chat = require('../models/Chat');

// Define the service function
async function markChatAsRead(userId, chatId) {
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

    // Set the isThereUnReadMessage flag to false for the user
    userInChat.isThereUnReadMessage = false;

    // Save the updated chat
    await chat.save();

    console.log('Chat marked as read successfully');
  } catch (error) {
    console.error('Error marking chat as read:', error);
  }
}



async function markChatAsUnRead(userId, chatId) {
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

    // Set the isThereUnReadMessage flag to false for the user
    userInChat.isThereUnReadMessage = true;

    // Save the updated chat
    await chat.save();

    console.log('Chat marked as read successfully');
  } catch (error) {
    console.error('Error marking chat as read:', error);
  }
}

module.exports = { markChatAsRead,markChatAsUnRead };
