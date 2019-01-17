import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {IconDashboard} from '../../../components/icons/IconDashboard';
import {IconMeter} from '../../../components/icons/IconMeter';
import {IconReport} from '../../../components/icons/IconReport';
import {Column} from '../../../components/layouts/column/Column';
import {FoldableMainMenuItem} from '../../../components/layouts/foldable/Foldable';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isReportPage} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {PathNamed} from '../../../types/Types';
import {SavedSelectionsContainer} from '../../sidemenu/containers/savedSelections/SavedSelectionsContainer';
import {SelectionTreeContainer} from '../../sidemenu/containers/selection-tree/SelectionTreeContainer';
import {mainMenuIconProps, MainMenuItem} from '../components/menu-items/MainMenuItem';

interface StateToProps extends PathNamed {
  isReportPage: boolean;
}

const MvpMainMenuItems = ({isReportPage, pathName}: StateToProps) => (
  <Column>
    <Link to={routes.dashboard} className="link">
      <MainMenuItem
        name={translate('dashboard')}
        isSelected={routes.dashboard === pathName || routes.home === pathName}
        icon={<IconDashboard {...mainMenuIconProps}/>}
      />
    </Link>
    <FoldableMainMenuItem
      containerClassName="FoldableMenuItem"
      icon={<IconMeter {...mainMenuIconProps}/>}
      isSelected={pathName === routes.meter}
      isVisible={pathName === routes.meter}
      linkTo={routes.meter}
      pathName={pathName}
      title={translate('meter')}
    >
      <SavedSelectionsContainer/>
    </FoldableMainMenuItem>
    <FoldableMainMenuItem
      containerClassName="FoldableMenuItem"
      icon={<IconReport {...mainMenuIconProps}/>}
      isSelected={pathName.startsWith(routes.report) && !pathName.includes('selection')}
      isVisible={pathName.startsWith(routes.report) && !pathName.includes('selection')}
      linkTo={routes.report}
      pathName={pathName}
      title={translate('report')}
    >
      {isReportPage && <SelectionTreeContainer/>}
    </FoldableMainMenuItem>
  </Column>
);

const mapStateToProps = ({routing}: RootState): StateToProps => ({
  pathName: getPathname(routing),
  isReportPage: isReportPage(routing),
});

export const MvpMainMenuItemsContainer = connect<StateToProps>(mapStateToProps)(MvpMainMenuItems);
