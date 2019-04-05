import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {seenNotifications} from '../../../state/ui/notifications/notificationsActions';
import {NotificationDispatchProps, Notifications, NotificationsProps} from '../component/Notifications';

const mapStateToProps = ({ui: {notifications: {hasNotifications}}}: RootState): NotificationsProps => ({
  hasNotifications,
});

const mapDispatchToProps = (dispatch): NotificationDispatchProps => bindActionCreators({
  seenNotifications,
}, dispatch);

export const NotificationsContainer =
  connect<NotificationsProps, NotificationDispatchProps>(mapStateToProps, mapDispatchToProps)(Notifications);
