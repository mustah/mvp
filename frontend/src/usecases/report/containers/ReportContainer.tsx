import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {selectReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {getSelectedIndicatorReport} from '../../../state/ui/indicator/indicatorSelectors';
import {paperStyle} from '../../app/themes';
import {IndicatorType} from '../../common/components/indicators/models/widgetModels';
import {
  SelectableIndicatorWidgets,
  SelectedIndicatorWidgetProps,
} from '../../common/components/indicators/SelectableIndicatorWidgets';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Titles';
import {PageContainer} from '../../common/containers/PageContainer';
import {PeriodContainer} from '../../common/containers/PeriodContainer';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import {GraphContainer} from './GraphContainer';
import {indicators} from '../models/reportModels';

interface StateToProps extends SelectedIndicatorWidgetProps {
  selectedWidget: any;
}

interface DispatchToProps {
  selectIndicatorWidget: (type: IndicatorType) => any;
}

const ReportContainer = (props: StateToProps & DispatchToProps & InjectedAuthRouterProps) => {
  const {selectedWidget, selectIndicatorWidget} = props;
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('report')}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <SelectableIndicatorWidgets
        indicators={indicators}
        selectedWidget={selectedWidget}
        selectIndicatorWidget={selectIndicatorWidget}
      />

      <Paper style={{...paperStyle, marginTop: 24}}>
        <GraphContainer/>
      </Paper>
    </PageContainer>
  );
};

const mapStateToProps = (state: RootState): StateToProps => {
  const {ui} = state;
  return {
    selectedWidget: getSelectedIndicatorReport(ui),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportContainer);
