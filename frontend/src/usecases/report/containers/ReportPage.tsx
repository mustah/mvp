import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {MeasurementContentContainer} from './MeasurementContentContainer';

const noop = () => null;

export const ReportPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <MainTitle>{translate('report')}</MainTitle>
      <Row>
        <SummaryContainer/>
      </Row>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <Tabs className="ReportTabs">
        <TabTopBar>
          <TabHeaders selectedTab={TabName.values} onChangeTab={noop}>
            <Tab tab={TabName.values} title={translate('measurements')}/>
          </TabHeaders>
        </TabTopBar>
        <MeasurementContentContainer/>
      </Tabs>
    </Paper>
  </PageLayout>
);
