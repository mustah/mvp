import * as React from 'react';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {TabOptionProps} from './TabOption';
import {TabUnderline} from './TabUnderliner';

interface TabOptionsProps {
  options: Array<React.ReactElement<TabOptionProps>>;
}

export const TabOptions = (props: TabOptionsProps) => {
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
