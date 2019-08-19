import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import SocialDomain from 'material-ui/svg-icons/social/domain';
import * as React from 'react';
import {connect} from 'react-redux';
import {routes} from '../../../app/routes';
import {withMvpAdminOnly} from '../../../components/hoc/withRoles';
import {IconMeter} from '../../../components/icons/IconMeter';
import {Column} from '../../../components/layouts/column/Column';
import {Link} from '../../../components/links/Link';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {mainMenuIconProps, MainMenuItem} from '../components/menu-items/MainMenuItem';

interface StateToProps {
  pathname: string;
}

const UsersLinkMenuItems = ({pathname}: StateToProps) => (
  <Link to={routes.admin}>
    <MainMenuItem
      name={translate('users')}
      isSelected={routes.admin === pathname || routes.adminUsers === pathname}
      icon={<ActionSupervisorAccount {...mainMenuIconProps}/>}
    />
  </Link>
);

const OrganisationLinkMenuItem = withMvpAdminOnly(({pathname}: StateToProps) => (
  <Link to={routes.adminOrganisations}>
    <MainMenuItem
      name={translate('organisations')}
      isSelected={routes.adminOrganisations === pathname}
      icon={<SocialDomain {...mainMenuIconProps}/>}
    />
  </Link>
));

const MeterDefinitionsLinkMenuItem = withMvpAdminOnly((props: StateToProps) => (
  <Link to={routes.adminMeterDefinitions}>
    <MainMenuItem
      name={translate('meter definitions')}
      isSelected={routes.adminMeterDefinitions === props.pathname}
      icon={<IconMeter {...mainMenuIconProps}/>}
    />
  </Link>
));

const MainMenuItems = (props: StateToProps) => (
  <Column>
    <UsersLinkMenuItems {...props} />
    <OrganisationLinkMenuItem {...props}/>
    <MeterDefinitionsLinkMenuItem {...props}/>
  </Column>
);

const mapStateToProps = ({router}: RootState): StateToProps => ({
  pathname: getPathname(router),
});

export const AdminMainMenuItemsContainer = connect(mapStateToProps)(MainMenuItems);
