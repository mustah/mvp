import Badge from 'material-ui/Badge';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {IconDashboard} from '../../../../components/icons/IconDashboard';
import {IconMeter} from '../../../../components/icons/IconMeter';
import {IconReport} from '../../../../components/icons/IconReport';
import {Column} from '../../../../components/layouts/column/Column';
import {FoldableMainMenuItem} from '../../../../components/layouts/foldable/FoldableMainMenuItem';
import {translate} from '../../../../services/translationService';
import {SavedSelectionsContainer} from '../../../sidemenu/containers/SavedSelectionsContainer';
import {ReportPageProps, StateToProps} from '../../containers/MvpMainMenuItemsContainer';
import {mainMenuIconProps, MainMenuItem} from './MainMenuItem';

const BadgeComponent = ({isReportPage, numSelectedItems}: ReportPageProps) => {
  const badgeStyle: React.CSSProperties = {padding: 12, left: isReportPage ? 124 : 127};
  return numSelectedItems > 0
    ? (
      <Badge
        badgeContent={numSelectedItems}
        secondary={true}
        style={badgeStyle}
        className="Animate-zoom-in"
        key={`badge-${numSelectedItems}`}
      />
    )
    : null;
};

export const MainMenuItems = ({isMeterPage, isReportPage, numSelectedItems, pathName}: StateToProps) => (
  <Column>
    <Link to={routes.dashboard} className="link">
      <MainMenuItem
        icon={<IconDashboard {...mainMenuIconProps}/>}
        isSelected={routes.dashboard === pathName || routes.home === pathName}
        name={translate('dashboard')}
      />
    </Link>
    <FoldableMainMenuItem
      containerClassName="FoldableMainMenuItem"
      icon={<IconMeter {...mainMenuIconProps}/>}
      isSelected={isMeterPage}
      isVisible={isMeterPage}
      title={translate('meter')}
    >
      <SavedSelectionsContainer/>
    </FoldableMainMenuItem>
    <Link to={routes.report} className="link">
      <MainMenuItem
        icon={<IconReport {...mainMenuIconProps}/>}
        isSelected={isReportPage}
        name={translate('report')}
      >
        <BadgeComponent isReportPage={isReportPage} numSelectedItems={numSelectedItems}/>
      </MainMenuItem>
    </Link>
  </Column>
);
