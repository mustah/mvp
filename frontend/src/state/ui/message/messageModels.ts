export interface MessageState {
  isOpen: boolean;
  message: string;
  messageType?: 'fail' | 'success';
}
