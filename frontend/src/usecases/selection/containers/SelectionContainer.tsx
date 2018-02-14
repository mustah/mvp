import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {SelectionContentContainer} from './SelectionContentContainer';

interface StateToProps {
  title: string;
}

type Props = StateToProps & InjectedAuthRouterProps;

const SelectionContainerComponent = (props: Props) => {

  const {title} = props;
  return (
    <MvpPageContainer>
      <Row className="space-between">
        <MainTitle>{title}</MainTitle>
        <Row>
          {/*<SummaryContainer/>*/}
          <PeriodContainer/>
        </Row>
      </Row>
      <Paper style={paperStyle}>
        <SelectionContentContainer/>
      </Paper>
    </MvpPageContainer>
  );
};

const mapStateToProps = ({searchParameters: {selection}}: RootState): StateToProps => {
  const title = selection.id === -1 ? translate('selection') : selection.name;
  return {
    title,
  };
};

export const SelectionContainer =
  connect<StateToProps>(mapStateToProps)(SelectionContainerComponent);
