import * as React from 'react';
import {WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {DashboardProps} from '../containers/DashboardContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {OverviewWidgets} from './widgets/OverviewWidgets';

export const Dashboard = ({dashboard, isFetching, meterMapMarkers}: DashboardProps) => {
  const widgets: WidgetModel[] = isFetching || !dashboard ? [] : dashboard.widgets;
  return (
    <PageComponent>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <Row className="Row-wrap-reverse">
        <MapWidgetContainer markers={meterMapMarkers}/>
        <OverviewWidgets widgets={widgets} isFetching={isFetching}/>
      </Row>
    </PageComponent>
  );
};
