import SocialNotifications from 'material-ui/svg-icons/social/notifications';
import * as React from 'react';
import {colors, iconStyle} from '../../../app/themes';
import {withContent} from '../../../components/hoc/withContent';
import {RowCenter} from '../../../components/layouts/row/Row';
import {firstUpperTranslated} from '../../../services/translationService';
import './Logo.scss';
import {TopMenuItem} from './TopMenuItem';

const NotificationIndicator = withContent(() => <div className="Notification"/>);

export interface NotificationsProps {
  hasNotifications: boolean;
}

export const Notifications = ({hasNotifications}: NotificationsProps) => (
  <TopMenuItem title={firstUpperTranslated('notifications')}>
    <RowCenter>
      <NotificationIndicator hasContent={hasNotifications}/>
      <SocialNotifications color={colors.white} style={iconStyle}/>
    </RowCenter>
  </TopMenuItem>
);
