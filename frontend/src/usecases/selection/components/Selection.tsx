import Paper from 'material-ui/Paper';
import * as React from 'react';
import {translate} from '../../../services/translationService';
import {paperStyle} from '../../app/themes';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Titles';
import {PageContainer} from '../../common/containers/PageContainer';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import {SelectionContentContainer} from '../containers/SelectionContentContainer';
import {SelectionOptionsLoaderContainer} from '../containers/SelectionOptionsLoaderContainer';

export const Selection = () => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('selection')}</MainTitle>
        <SummaryContainer/>
      </Row>

      <SelectionOptionsLoaderContainer>
        <Paper style={paperStyle}>
          <SelectionContentContainer/>
        </Paper>
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};
