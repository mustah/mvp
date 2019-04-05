import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {Notifications, NotificationsProps} from '../component/Notifications';

const mapStateToProps = (_: RootState): NotificationsProps => ({
  hasNotifications: false,
});

export const NotificationsContainer = connect<NotificationsProps>(mapStateToProps)(Notifications);
