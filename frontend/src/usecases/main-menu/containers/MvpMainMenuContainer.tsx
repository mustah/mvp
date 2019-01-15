import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {IconDashboard} from '../../../components/icons/IconDashboard';
import {IconMeter} from '../../../components/icons/IconMeter';
import {IconReport} from '../../../components/icons/IconReport';
import {Column} from '../../../components/layouts/column/Column';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {MainMenuWrapper} from '../components/main-menu-wrapper/MainMenuWrapper';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
}

const MvpMainMenu = ({pathname}: StateToProps) => (
  <MainMenuWrapper>
    <Column>
      <Link to={routes.dashboard} className="link">
        <MenuItem
          name={translate('dashboard')}
          isSelected={routes.dashboard === pathname || routes.home === pathname}
          icon={<IconDashboard className="MenuItem-icon"/>}
        />
      </Link>
      <Link to={routes.meter} className="link">
        <MenuItem
          name={translate('meter')}
          isSelected={routes.meter === pathname}
          icon={<IconMeter className="MenuItem-icon"/>}
        />
      </Link>
      <Link to={routes.report} className="link">
        <MenuItem
          name={translate('report')}
          isSelected={pathname.startsWith(routes.report) && !pathname.includes('selection')}
          icon={<IconReport className="MenuItem-icon"/>}
        />
      </Link>
    </Column>
  </MainMenuWrapper>
);

const mapStateToProps = ({routing}: RootState): StateToProps => ({
  pathname: getPathname(routing),
});

export const MvpMainMenuContainer = connect<StateToProps>(mapStateToProps)(MvpMainMenu);
