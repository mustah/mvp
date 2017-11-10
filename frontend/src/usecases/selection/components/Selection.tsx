import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../app/themes';
import {PageContainer} from '../../common/containers/PageContainer';
import {SelectionOptionsLoaderContainer} from '../containers/SelectionOptionsLoaderContainer';
import {SelectionContentContainer} from '../containers/SelectionContentContainer';
import {translate} from '../../../services/translationService';
import {MainTitle} from '../../common/components/texts/Title';
import {Row} from '../../common/components/layouts/row/Row';

export const Selection = () => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('selection')}</MainTitle>
      </Row>

      <SelectionOptionsLoaderContainer>
        <Paper style={paperStyle}>
          <SelectionContentContainer/>
        </Paper>
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};
