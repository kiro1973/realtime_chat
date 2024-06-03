const Chat = require('./Chat'); // Assuming Chat model is defined in a file named Chat.js

async function updateUserUnreadMessageStatus(userId,chatId) {
  try {


    const updatedChat = await Chat.findByIdAndUpdate(
      chatId,
      { $set: { "users.$[elem].isThereUnReadMessage": true } },
      { new: true, arrayFilters: [{ "elem.userId": userId }] }
    );
    // const updatedChat = await Chat.findOneAndUpdate(
    //   { "users.userId": userId },
    //   { $set: { "users.$.isThereUnReadMessage": booleanValueOfReadStatus } },
    //   { new: true }
    // );

    return updatedChat;
  } catch (error) {
    console.error("Error updating chat:", error);
    throw error; // Rethrow the error for handling elsewhere
  }
}

module.exports = {
  updateUserUnreadMessageStatus
};