import * as React from 'react';
import {WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {IgnoreSelectionSummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {DashboardProps} from '../containers/DashboardContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {OverviewWidgets} from './widgets/OverviewWidgets';

export const Dashboard = ({dashboard, isFetching, meterMapMarkers}: DashboardProps) => {
  const widgets: WidgetModel[] = isFetching || !dashboard ? [] : dashboard.widgets;
  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <Row>
          <IgnoreSelectionSummaryContainer/>
        </Row>
      </Row>

      <Row className="Row-wrap-reverse">
        <MapWidgetContainer markers={meterMapMarkers}/>
        <OverviewWidgets widgets={widgets} isFetching={isFetching}/>
      </Row>
    </PageLayout>
  );
};
