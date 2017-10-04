import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {TabOptionProps} from '../components/tabOption/TabOption';
import {TabUnderline} from '../components/tabUnderline/TabUnderliner';

interface TabOptionsContainerProps {
  options: TabOptionProps[];
}

export const TabOptionsContainer = (props: TabOptionsContainerProps) => {
  const {options} = props;
  return (
    <Column className={'flex-1'}>
      <Row className={'Row-center'}>
        {options}
      </Row>
      <TabUnderline/>
    </Column>
  );
};
