import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {mainContentPaperStyle} from '../../../app/themes';
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

const SelectionContainerComponent = ({title}: StateToProps) => (
  <MvpPageContainer>
    <Row className="space-between">
      <MainTitle>{title}</MainTitle>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>
    <Paper style={mainContentPaperStyle}>
      <SelectionContentContainer/>
    </Paper>
  </MvpPageContainer>
);

const mapStateToProps = ({userSelection: {userSelection}}: RootState): StateToProps => ({
  title: userSelection.id === -1 ? translate('selection') : userSelection.name,
});

export const SelectionContainer =
  connect<StateToProps>(mapStateToProps)(SelectionContainerComponent);
