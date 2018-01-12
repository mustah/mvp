import Snackbar from 'material-ui/Snackbar';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {hideMessage} from '../../state/ui/message/messageActions';

interface StateToProps {
  message?: string;
  isOpen: boolean;
}

interface DispatchToProps {
  hideMessage: (reason: string) => void;
}

const MessageComponent = ({message = '', isOpen, hideMessage}: StateToProps & DispatchToProps) => (
  <Snackbar
    autoHideDuration={4000}
    message={message}
    onRequestClose={hideMessage}
    open={isOpen}
  />
);

const mapStateToProps = ({ui: {message: {message, isOpen}}}: RootState): StateToProps => ({
  message,
  isOpen,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  hideMessage,
}, dispatch);

export const MessageContainer = connect(mapStateToProps, mapDispatchToProps)(MessageComponent);
