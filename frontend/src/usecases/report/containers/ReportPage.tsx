import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {translate} from '../../../services/translationService';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {ReportMeasurementsContentContainer} from './ReportMeasurementsContentContainer';

const noop = () => null;

export const ReportPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <MainTitle>{translate('report')}</MainTitle>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <Tabs className="ReportTabs">
        <TabTopBar>
          <TabHeaders selectedTab={TabName.values} onChangeTab={noop}>
            <Tab tab={TabName.values} title={translate('measurements')}/>
          </TabHeaders>
        </TabTopBar>
        <ReportMeasurementsContentContainer/>
      </Tabs>
    </Paper>
  </PageLayout>
);
