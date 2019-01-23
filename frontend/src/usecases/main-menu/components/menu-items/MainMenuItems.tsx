import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {IconDashboard} from '../../../../components/icons/IconDashboard';
import {IconMeter} from '../../../../components/icons/IconMeter';
import {IconReport} from '../../../../components/icons/IconReport';
import {Column} from '../../../../components/layouts/column/Column';
import {FoldableMainMenuItem} from '../../../../components/layouts/foldable/FoldableMainMenuItem';
import {InfoText} from '../../../../components/texts/Texts';
import {translate} from '../../../../services/translationService';
import {SavedSelectionsContainer} from '../../../sidemenu/containers/SavedSelectionsContainer';
import {MainMenuItemProps} from '../../containers/MvpMainMenuItemsContainer';
import {mainMenuIconProps, MainMenuItem} from './MainMenuItem';

const infoTextStyle: React.CSSProperties = {paddingTop: 8, paddingLeft: 16};

export const MainMenuItems = ({isMeterPage, isReportPage, pathName, resetSelection}: MainMenuItemProps) => {
  const canShowReportMenuItemContent = isReportPage && !pathName.includes('selection');
  const canShowMeterMenuItemContent = isMeterPage;

  return (
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
        isSelected={canShowMeterMenuItemContent}
        isVisible={canShowMeterMenuItemContent}
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
        isSelected={canShowReportMenuItemContent}
        isVisible={canShowReportMenuItemContent}
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
};
