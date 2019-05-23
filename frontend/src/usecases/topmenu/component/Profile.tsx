import MenuItem from 'material-ui/MenuItem';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import ActionExitToApp from 'material-ui/svg-icons/action/exit-to-app';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {routes} from '../../../app/routes';
import {iconStyle, topMenuInnerDivStyle, topMenuItemDivStyle, topMenuItemIconStyle} from '../../../app/themes';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {RowCenter} from '../../../components/layouts/row/Row';
import {Link} from '../../../components/links/Link';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {TopMenuItem} from './TopMenuItem';

const makeAvatarTitle = ({name, email}: User): string => `${name} (${email})`;

const makeAvatarIcon = (user: User) => (props: Clickable) => (
  <ColumnCenter {...props} title={makeAvatarTitle(user)}>
    <RowCenter>
      <ActionAccountCircle color={colors.white} style={iconStyle}/>
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

  const asyncLogout = async () => {
    await logout();
  };

  const renderPopoverContent: RenderFunction<OnClick> = () => (
    <>
      <Link to={routes.userProfile} key="goToProfile">
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
        onClick={asyncLogout}
      >
        {translate('logout')}
      </MenuItem>
    </>
  );

  return (
    <TopMenuItem>
      <PopoverMenu
        className="Row-center"
        IconComponent={Icon}
        anchorOrigin={anchorOrigin}
        targetOrigin={targetOrigin}
        renderPopoverContent={renderPopoverContent}
      />
    </TopMenuItem>
  );
};
