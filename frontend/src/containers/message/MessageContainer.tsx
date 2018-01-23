import Snackbar from 'material-ui/Snackbar';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {hideMessage} from '../../state/ui/message/messageActions';
import {MessageState} from '../../state/ui/message/messageModels';

type StateToProps = MessageState;

interface DispatchToProps {
  hideMessage: (reason: string) => void;
}

const messageStyles: {[key: string]: React.CSSProperties} = {
  fail: {backgroundColor: 'red'},
  success: {backgroundColor: 'green'},
};

const MessageComponent = ({message = '', isOpen, hideMessage, messageType}: StateToProps & DispatchToProps) => (
  <Snackbar
    autoHideDuration={4000}
    message={message}
    onRequestClose={hideMessage}
    open={isOpen}
    bodyStyle={messageStyles[messageType]}
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
