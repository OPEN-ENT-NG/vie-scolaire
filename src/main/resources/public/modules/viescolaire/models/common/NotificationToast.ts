export class NotificationToast {
    text: String;
    status: String;

    /**
     *
     * @param {String} text
     * @param {String} status (can be info, confirm)
     */
    constructor(text: String, status: String = 'info') {
        this.text = text;
        this.status = status;
    }
}