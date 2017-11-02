import * as React from 'react';
import {PageContainer} from '../../common/containers/PageContainer';
import {SelectionContentBoxContainer} from '../components/SelectionContentBox';
import {SelectionOptionsLoaderContainer} from './SelectionOptionsLoaderContainer';

export const SelectionPageComponent = () => {
  return (
    <PageContainer>
      <SelectionOptionsLoaderContainer>
        <SelectionContentBoxContainer/>
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );
};
