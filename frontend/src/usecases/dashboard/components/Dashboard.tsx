import * as React from 'react';
import {IconDashboard} from '../../../components/icons/IconDashboard';
import {WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {IgnoreSelectionSummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {DashboardProps} from '../containers/DashboardContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {OverviewWidgets} from './widgets/OverviewWidgets';
import {EmptyWidget} from './widgets/Widget';

export const Dashboard = ({dashboard, isFetching, meterMapMarkers}: DashboardProps) => {
  const widgets: WidgetModel[] = isFetching || !dashboard ? [] : dashboard.widgets;
  const wideWidgetColumnStyle: React.CSSProperties = {marginRight: 24};
  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <Row>
          <IgnoreSelectionSummaryContainer/>
        </Row>
      </Row>

      <Row className="Row-wrap-reverse">
        <Column style={wideWidgetColumnStyle}>
          <MapWidgetContainer markers={meterMapMarkers}/>
          <EmptyWidget icon={<IconDashboard style={{height: 32, width: 32}}/>} style={{height: 100}}/>
        </Column>
        <OverviewWidgets widgets={widgets} isFetching={isFetching}/>
      </Row>
    </PageLayout>
  );
};
