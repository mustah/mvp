import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {PageContainer} from '../../../containers/PageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchSelections} from '../../../state/domain-models/domainModelsActions';
import {Callback} from '../../../types/Types';
import {SelectionContentContainer} from './SelectionContentContainer';

interface StateToProps {
  title: string;
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSelections: Callback;
}

type Props = StateToProps & InjectedAuthRouterProps & DispatchToProps;

class SelectionContainerComponent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchSelections();
  }

  render() {
    const {isFetching, title} = this.props;
    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{title}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <Loader isFetching={isFetching}>
          <Paper style={paperStyle}>
            <SelectionContentContainer/>
          </Paper>
        </Loader>
      </PageContainer>
    );
  }
}

const mapStateToProps = ({searchParameters, domainModels: {cities, addresses, alarms}}: RootState): StateToProps => {
  const {selection} = searchParameters;
  const title = selection.id === -1 ? translate('selection') : selection.name;
  return {
    title,
    isFetching: cities.isFetching || addresses.isFetching || alarms.isFetching,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSelections,
}, dispatch);

export const SelectionContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContainerComponent);
