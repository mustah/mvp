import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {SelectionContentContainer} from './SelectionContentContainer';

interface StateToProps {
  title: string;
}

type Props = StateToProps & InjectedAuthRouterProps;

const SelectionContainerComponent = ({title}: Props) => {
  return (
    <MvpPageContainer>
      <Row className="space-between">
        <MainTitle>{title}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>
      <Paper style={paperStyle}>
        <SelectionContentContainer/>
      </Paper>
    </MvpPageContainer>
  );
};

const mapStateToProps = ({userSelection: {userSelection}}: RootState): StateToProps => {
  const title = userSelection.id === -1 ? translate('selection') : userSelection.name;
  return {title};
};

export const SelectionContainer =
  connect<StateToProps>(mapStateToProps)(SelectionContainerComponent);
