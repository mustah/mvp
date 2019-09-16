import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {SelectionContentContainer} from './SelectionContentContainer';
import {SelectionMenuContainer} from './SelectionMenuContainer';

export const SelectionPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <SelectionMenuContainer/>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <SelectionContentContainer/>
    </Paper>
  </PageLayout>
);
