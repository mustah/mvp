export type MessageType = 'fail' | 'success';

export interface MessageState {
  isOpen: boolean;
  message: string;
  messageType?: MessageType;
}
