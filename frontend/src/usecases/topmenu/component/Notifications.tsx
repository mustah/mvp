import {default as classNames} from 'classnames';
import SocialNotifications from 'material-ui/svg-icons/social/notifications';
import SocialPages from 'material-ui/svg-icons/social/pages';
import * as React from 'react';
import {linkToReleaseNotes} from '../../../app/routes';
import {colors, iconStyle, topMenuItemIconStyle} from '../../../app/themes';
import {withContent} from '../../../components/hoc/withContent';
import {RowCenter} from '../../../components/layouts/row/Row';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {Large} from '../../../components/texts/Texts';
import {config} from '../../../config/config';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {OnClick, OnClickWith, RenderFunction} from '../../../types/Types';
import {HrefMenuItem} from './LinkMenuItem';
import {TopMenuItem} from './TopMenuItem';

const NotificationIndicator = withContent(() => <div className="Notification"/>);

const PopoverIcon = () => <SocialNotifications color={colors.white} style={iconStyle}/>;

export interface NotificationsProps {
  hasNotifications: boolean;
}

export interface NotificationDispatchProps {
  seenNotifications: OnClickWith<string>;
}

type Props = NotificationsProps & NotificationDispatchProps;

export const Notifications = ({hasNotifications, seenNotifications}: Props) => {
  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickToReleaseNotes: OnClick = () => {
      seenNotifications(config().frontendVersion);
      onClick();
    };
    const iconColor: string = hasNotifications ? colors.black : colors.menuItemLeftIcon;

    return ([
        (
          <HrefMenuItem
            className="Notifications-MenuItem"
            onClick={onClickToReleaseNotes}
            leftIcon={<SocialPages style={topMenuItemIconStyle} color={iconColor}/>}
            to={linkToReleaseNotes}
            target="_blank"
            key="link-to-release-notes"
          >
            <Large className={classNames({Bold: hasNotifications})}>{translate('what\'s new')}?</Large>
          </HrefMenuItem>
        ),
      ]
    );
  };
  return (
    <TopMenuItem title={firstUpperTranslated('notifications')}>
      <RowCenter>
        <NotificationIndicator hasContent={hasNotifications}/>
        <PopoverMenu
          className="Row-center"
          IconComponent={PopoverIcon}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          renderPopoverContent={renderPopoverContent}
        />
      </RowCenter>
    </TopMenuItem>
  );
};
