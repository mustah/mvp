import Snackbar from 'material-ui/Snackbar';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {colors} from '../../app/themes';
import {RootState} from '../../reducers/rootReducer';
import {hideMessage} from '../../state/ui/message/messageActions';
import {MessageState, MessageType} from '../../state/ui/message/messageModels';

type StateToProps = MessageState;

interface DispatchToProps {
  hideMessage: (reason: string) => void;
}

const messageStyles: {[key in MessageType]: React.CSSProperties} = {
  fail: {backgroundColor: colors.red},
  success: {backgroundColor: colors.darkGreen},
};

const MessageComponent = ({message = '', isOpen, hideMessage, messageType}: StateToProps & DispatchToProps) => (
  <Snackbar
    autoHideDuration={4000}
    message={message}
    onRequestClose={hideMessage}
    open={isOpen}
    bodyStyle={messageType ? messageStyles[messageType] : {}}
  />
);

const mapStateToProps = ({ui: {message: {message, isOpen, messageType}}}: RootState): StateToProps => ({
  message,
  isOpen,
  messageType,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  hideMessage,
}, dispatch);

export const MessageContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MessageComponent);
