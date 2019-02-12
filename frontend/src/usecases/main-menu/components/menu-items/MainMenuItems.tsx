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
import {StateToProps} from '../../containers/MvpMainMenuItemsContainer';
import {mainMenuIconProps, MainMenuItem} from './MainMenuItem';

export const MainMenuItems = ({isMeterPage, isReportPage, pathName}: StateToProps) => (
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
        isSelected={isReportPage && !pathName.includes('selection')}
        name={translate('report')}
      />
    </Link>
  </Column>
);
