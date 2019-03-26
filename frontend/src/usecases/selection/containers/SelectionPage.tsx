import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {SelectionContentContainer} from './SelectionContentContainer';
import {SelectionMenuContainer} from './SelectionMenuContainer';

export const SelectionPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <SelectionMenuContainer/>
      <Row>
        <SummaryContainer/>
      </Row>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <SelectionContentContainer/>
    </Paper>
  </PageLayout>
);
