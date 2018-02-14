import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import SocialDomain from 'material-ui/svg-icons/social/domain';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, iconStyle} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {isSuperAdminSelector} from '../../../services/authService';
import {translate} from '../../../services/translationService';
import {MainMenuWrapper} from '../components/main-menu-wrapper/MainMenuWrapper';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
  isSuperAdmin: boolean;
}

const AdminMainMenu = ({pathname, isSuperAdmin}: StateToProps) => {

  const organisaionMenuItem = isSuperAdmin ? (
    <Link to={routes.adminOrganisations} className="link">
      <MenuItem
        name={translate('organisations')}
        isSelected={routes.adminOrganisations === pathname}
        icon={<SocialDomain style={iconStyle} color={colors.white} className="MenuItem-icon"/>}
      />
    </Link>
  ) : null;

  const usersMenuItem = (
    <Link to={routes.admin} className="link">
      <MenuItem
        name={translate('users')}
        isSelected={routes.admin === pathname || routes.adminUsers === pathname}
        icon={<ActionSupervisorAccount style={iconStyle} color={colors.white} className="MenuItem-icon"/>}
      />
    </Link>
  );

  return (
    <MainMenuWrapper>
      <Column>
        {usersMenuItem}
        {organisaionMenuItem}
      </Column>
    </MainMenuWrapper>
  );
};

const mapStateToProps = ({routing, ...restOfRootState}: RootState): StateToProps => {
  return {
    pathname: getPathname(routing),
    isSuperAdmin: isSuperAdminSelector({routing, ...restOfRootState}),
  };
};

export const AdminMainMenuContainer =
  connect<StateToProps>(mapStateToProps)(AdminMainMenu);
