import {default as classNames} from 'classnames';
import SocialNotifications from 'material-ui/svg-icons/social/notifications';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {linkToReleaseNotes} from '../../../app/routes';
import {iconStyle} from '../../../app/themes';
import {withContent} from '../../../components/hoc/withContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {IconDuck} from '../../../components/icons/IconDuck';
import {RowCenter} from '../../../components/layouts/row/Row';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {Large} from '../../../components/texts/Texts';
import {config} from '../../../config/config';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {OnClick, OnClickWith, RenderFunction} from '../../../types/Types';
import {HrefMenuItem} from './LinkMenuItem';
import {TopMenuItem} from './TopMenuItem';

const duckIconStyle: React.CSSProperties = {
  padding: '0 0 0 1px',
  margin: '2px 0 0 2px',
  width: 26,
  height: 26,
};

const Notification = withCssStyles(({cssStyles: {primary}}: ThemeContext) => {
  const style = {
    border: '1px solid',
    background: colors.notification,
    borderColor: primary.bgDark,
  };
  return <div className="Notification" style={style}/>;
});

const NotificationIndicator = withContent(Notification);

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

    return ([
        (
          <HrefMenuItem
            className="Notifications-MenuItem"
            onClick={onClickToReleaseNotes}
            leftIcon={<IconDuck style={duckIconStyle} color={colors.black}/>}
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
      <RowCenter className="Notifications">
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
