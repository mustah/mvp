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
import {MainMenuItem} from '../components/menuitems/MainMenuItem';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface StateToProps {
  pathname: string;
  user: User;
}

const iconProps: SvgIconProps = {
  style: {...iconStyle, width: 26, height: 26},
  color: colors.black,
  className: 'MainMenuItem-icon',
};

const AdminOrganisationLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.adminOrganisations} className="link">
    <MainMenuItem
      name={translate('organisations')}
      isSelected={routes.adminOrganisations === pathname}
      icon={<SocialDomain {...iconProps}/>}
    />
  </Link>
);

const OrganisationMenuItem = superAdminOnly(AdminOrganisationLinkMenuItem);

const AdminMainMenuItems = (props: StateToProps) => (
  <Column>
    <Link to={routes.admin} className="link">
      <MainMenuItem
        name={translate('users')}
        isSelected={routes.admin === props.pathname || routes.adminUsers === props.pathname}
        icon={<ActionSupervisorAccount {...iconProps}/>}
      />
    </Link>
    <OrganisationMenuItem {...props}/>
  </Column>
);

const mapStateToProps = ({routing, auth}: RootState): StateToProps => ({
  pathname: getPathname(routing),
  user: getUser(auth),
});

export const AdminMainMenuItemsContainer =
  connect<StateToProps>(mapStateToProps)(AdminMainMenuItems);
