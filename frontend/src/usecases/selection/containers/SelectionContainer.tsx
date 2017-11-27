import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageContainer} from '../../../containers/PageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {SelectionContentContainer} from './SelectionContentContainer';
import {SelectionOptionsLoaderContainer} from './SelectionOptionsLoaderContainer';

interface StateToProps {
  title: string;
}

export const SelectionContainerComponent = (props: StateToProps & InjectedAuthRouterProps) => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{props.title}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <SelectionOptionsLoaderContainer>
        <Paper style={paperStyle}>
          <SelectionContentContainer/>
        </Paper>
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};

const mapStateToProps = ({searchParameters: {selection}}: RootState): StateToProps => {
  const title = selection.id === -1 ? translate('selection') : selection.name;
  return {
    title,
  };
};

export const SelectionContainer = connect<StateToProps>(mapStateToProps)(SelectionContainerComponent);
