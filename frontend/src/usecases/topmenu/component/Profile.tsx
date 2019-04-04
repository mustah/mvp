import MenuItem from 'material-ui/MenuItem';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import ActionExitToApp from 'material-ui/svg-icons/action/exit-to-app';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {topMenuInnerDivStyle, topMenuItemDivStyle, topMenuItemIconStyle} from '../../../app/themes';
import {IconAvatar} from '../../../components/icons/IconAvatar';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {MenuUnderline} from './MenuUnderline';
import './Profile.scss';

const makeAvatarTitle = ({name, email}: User): string => `${name} (${email})`;

const makeAvatarIcon = (user: User) => (props: Clickable) => (
  <ColumnCenter {...props} title={makeAvatarTitle(user)}>
    <RowCenter>
      <IconAvatar/>
    </RowCenter>
  </ColumnCenter>
);

export interface StateToProps {
  user: User;
}

export interface DispatchToProps {
  logout: OnClick;
}

type Props = StateToProps & DispatchToProps;

export const Profile = ({user, logout}: Props) => {
  const Icon = makeAvatarIcon(user);

  const renderPopoverContent: RenderFunction<OnClick> = () => (
    <>
      <Link to={routes.userProfile} className="link" key="goToProfile">
        <MenuItem
          innerDivStyle={topMenuInnerDivStyle}
          leftIcon={<ActionAccountCircle style={topMenuItemIconStyle}/>}
          style={topMenuItemDivStyle}
          className="first-uppercase"
        >
          {translate('profile')}
        </MenuItem>
      </Link>
      <MenuItem
        innerDivStyle={topMenuInnerDivStyle}
        leftIcon={<ActionExitToApp style={{...topMenuItemIconStyle, transform: 'rotate(180deg)'}}/>}
        style={topMenuItemDivStyle}
        className="first-uppercase"
        onClick={logout}
      >
        {translate('logout')}
      </MenuItem>
    </>
  );

  return (
    <Column className="ProfileContainer TopMenu-Item">
      <Row className="Profile">
        <PopoverMenu
          IconComponent={Icon}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          renderPopoverContent={renderPopoverContent}
        />
      </Row>
      <MenuUnderline/>
    </Column>
  );
};
