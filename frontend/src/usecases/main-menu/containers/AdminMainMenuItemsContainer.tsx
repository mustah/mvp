import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import SocialDomain from 'material-ui/svg-icons/social/domain';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {IconMeter} from '../../../components/icons/IconMeter';
import {Column} from '../../../components/layouts/column/Column';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {getUser} from '../../auth/authSelectors';
import {mainMenuIconProps, MainMenuItem} from '../components/menu-items/MainMenuItem';

interface StateToProps {
  pathname: string;
  user: User;
}

const AdminOrganisationLinkMenuItem = ({pathname}: StateToProps) => (
  <Link to={routes.adminOrganisations} className="link">
    <MainMenuItem
      name={translate('organisations')}
      isSelected={routes.adminOrganisations === pathname}
      icon={<SocialDomain {...mainMenuIconProps}/>}
    />
  </Link>
);

const AdminMainMenuItems = (props: StateToProps) => (
  <Column>
    <Link to={routes.admin} className="link">
      <MainMenuItem
        name={translate('users')}
        isSelected={routes.admin === props.pathname || routes.adminUsers === props.pathname}
        icon={<ActionSupervisorAccount {...mainMenuIconProps}/>}
      />
    </Link>
    <AdminOrganisationLinkMenuItem {...props}/>
    <Link to={routes.adminMeterDefinitions} className="link">
      <MainMenuItem
        name={translate('meter definitions')}
        isSelected={routes.adminMeterDefinitions === props.pathname}
        icon={<IconMeter {...mainMenuIconProps}/>}
      />
    </Link>
  </Column>
);

const mapStateToProps = ({routing, auth}: RootState): StateToProps => ({
  pathname: getPathname(routing),
  user: getUser(auth),
});

export const AdminMainMenuItemsContainer =
  connect<StateToProps>(mapStateToProps)(AdminMainMenuItems);
