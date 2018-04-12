import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {HasContent} from '../../../components/content/HasContent';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {
  SelectableIndicatorWidgets,
  SelectedIndicatorWidgetProps,
} from '../../../components/indicators/SelectableIndicatorWidgets';
import {Row} from '../../../components/layouts/row/Row';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {selectReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {getSelectedIndicatorTypeForReport} from '../../../state/ui/indicator/indicatorSelectors';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {indicators} from '../reportModels';
import {GraphContainer} from './GraphContainer';

interface DispatchToProps {
  selectIndicatorWidget: OnSelectIndicator;
}

type Props = SelectedIndicatorWidgetProps & DispatchToProps & InjectedAuthRouterProps;

const style: React.CSSProperties = {width: '100%', height: '100%'};
const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 24};
const selectedTab: TabName = TabName.graph;

const ReportComponent = ({selectedIndicatorType, selectIndicatorWidget}: Props) => {
  const onChangeTab = () => void(0);
  return (
    <MvpPageContainer>
      <Row className="space-between">
        <MainTitle>{translate('report')}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <SelectableIndicatorWidgets
        indicators={indicators}
        selectedIndicatorType={selectedIndicatorType}
        selectIndicatorWidget={selectIndicatorWidget}
      />

      <Paper style={contentStyle}>
        <div style={style}>
          <Tabs>
            <TabTopBar>
              <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
                <Tab tab={TabName.graph} title={translate('graph')}/>
                <Tab tab={TabName.table} title={translate('table')}/>
              </TabHeaders>
              <TabSettings/>
            </TabTopBar>
            <TabContent tab={TabName.graph} selectedTab={selectedTab}>
              <GraphContainer/>
            </TabContent>
            <TabContent tab={TabName.table} selectedTab={selectedTab}>
              <HasContent
                hasContent={false}
                fallbackContent={<div/>}
              >
                <p>TBD</p>
              </HasContent>
            </TabContent>
          </Tabs>
        </div>
      </Paper>
    </MvpPageContainer>
  );
};

const mapStateToProps = ({ui}: RootState): SelectedIndicatorWidgetProps => ({
  selectedIndicatorType: getSelectedIndicatorTypeForReport(ui),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<SelectedIndicatorWidgetProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
