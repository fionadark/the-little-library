/**
 * Display a message to the user
 * @param {string} message - The message to display
 * @param {string} type - The message type ('error', 'success', 'info')
 */
export function showMessage(message, type = 'error') {
    const messageDiv = document.getElementById('message');
    if (!messageDiv) {
        console.error('Message div not found');
        return;
    }
    
    messageDiv.className = 'message ' + type;
    messageDiv.textContent = message;
    
    setTimeout(() => {
        messageDiv.textContent = '';
        messageDiv.className = '';
    }, 5000);
}
