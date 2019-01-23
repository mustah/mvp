import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {IconDashboard} from '../../../components/icons/IconDashboard';
import {IconMeter} from '../../../components/icons/IconMeter';
import {IconReport} from '../../../components/icons/IconReport';
import {Column} from '../../../components/layouts/column/Column';
import {FoldableMainMenuItem} from '../../../components/layouts/foldable/FoldableMainMenuItem';
import {InfoText} from '../../../components/texts/Texts';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isReportPage} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {resetSelection} from '../../../state/user-selection/userSelectionActions';
import {OnClick, PathNamed} from '../../../types/Types';
import {SavedSelectionsContainer} from '../../sidemenu/containers/SavedSelectionsContainer';
import {mainMenuIconProps, MainMenuItem} from '../components/menu-items/MainMenuItem';

interface StateToProps extends PathNamed {
  isReportPage: boolean;
}

interface DispatchToProps {
  resetSelection: OnClick;
}

type Props = StateToProps & DispatchToProps;

const infoTextStyle: React.CSSProperties = {paddingTop: 8, paddingLeft: 16};

const MvpMainMenuItems = ({isReportPage, pathName, resetSelection}: Props) => (
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
      onClick={resetSelection}
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
      <InfoText className="first-uppercase" style={infoTextStyle}>
        {translate('your saved reports will appear here')}
      </InfoText>
    </FoldableMainMenuItem>
  </Column>
);

const mapStateToProps = ({routing}: RootState): StateToProps => ({
  pathName: getPathname(routing),
  isReportPage: isReportPage(routing),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  resetSelection,
}, dispatch);

export const MvpMainMenuItemsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MvpMainMenuItems);
