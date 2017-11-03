import * as React from 'react';
import {PageContainer} from '../../common/containers/PageContainer';
import {SelectionContentBoxContainer} from './SelectionContentBox';
import {SelectionOptionsLoaderContainer} from '../containers/SelectionOptionsLoaderContainer';

export const Selection = () => {
  return (
    <PageContainer>
      <SelectionOptionsLoaderContainer>
        <SelectionContentBoxContainer/>
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};
