import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import SocialDomain from 'material-ui/svg-icons/social/domain';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, iconStyle} from '../../../app/themes';
import {superAdminOnly} from '../../../components/hoc/withRoles';
import {Column} from '../../../components/layouts/column/Column';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {getUser} from '../../auth/authSelectors';
import {MainMenu} from '../components/main-menu/MainMenu';
import {MenuItem} from '../components/menuitems/MenuItem';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface StateToProps {
  pathname: string;
  user: User;
}

const iconProps: SvgIconProps = {
  style: iconStyle,
  color: colors.white,
  className: 'MenuItem-icon',
};

const AdminOrganisationLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.adminOrganisations} className="link">
    <MenuItem
      name={translate('organisations')}
      isSelected={routes.adminOrganisations === pathname}
      icon={<SocialDomain {...iconProps}/>}
    />
  </Link>
);

const OrganisationMenuItem = superAdminOnly(AdminOrganisationLinkMenuItem);

const AdminMainMenu = (props: StateToProps) => (
  <MainMenu>
    <Column>
      <Link to={routes.admin} className="link">
        <MenuItem
          name={translate('users')}
          isSelected={routes.admin === props.pathname || routes.adminUsers === props.pathname}
          icon={<ActionSupervisorAccount {...iconProps}/>}
        />
      </Link>
      <OrganisationMenuItem {...props}/>
    </Column>
  </MainMenu>
);

const mapStateToProps = ({routing, auth}: RootState): StateToProps => ({
  pathname: getPathname(routing),
  user: getUser(auth),
});

export const AdminMainMenuContainer =
  connect<StateToProps>(mapStateToProps)(AdminMainMenu);
