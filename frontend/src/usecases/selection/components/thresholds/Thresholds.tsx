import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {TextFieldInput} from '../../../../components/inputs/TextFieldInput';
import {Row, RowMiddle} from '../../../../components/layouts/row/Row';
import {Medium} from '../../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../../services/translationService';
import {Quantity, quantityUnits} from '../../../../state/ui/graph/measurement/measurementModels';
import {ClassNamed, Styled} from '../../../../types/Types';
import '../SelectionResultList.scss';
import {DropDownMenu} from './DropDownMenu';

const dropDownStyle: React.CSSProperties = {
  fontSize: 16,
};

const quantityDropDownStyle: React.CSSProperties = {
  ...dropDownStyle,
  width: 256,
};

const operatorDropDownStyle: React.CSSProperties = {
  ...dropDownStyle,
  width: 124
};

const textFieldStyle: React.CSSProperties = {
  width: 114,
  marginBottom: 0,
  marginTop: 8,
};

// TODO[!must!] this will be the map to use when creating the search parameters! Please do not remove the unused
// properties here right now.
enum Operator {
  lt = '<',
  lte = '<=',
  gt = '>',
  gte = '>=',
}

interface Value {
  value: Operator | Quantity;
  text: string;
}

const makeMenuItem = ({value, text}: Value) => (
  <MenuItem key={text} primaryText={text} value={value}/>
);

const operatorMenuItems = Object.keys(Operator)
  .map((key): Value => ({value: key as Operator, text: Operator[key]}))
  .map(makeMenuItem);

const quantityMenuItems = Object.keys(Quantity)
  .map((key): Value => ({value: key as Quantity, text: Quantity[key]}))
  .sort((a, b) => a.text.localeCompare(b.text))
  .map(makeMenuItem);

type Props = ClassNamed & Styled;

export const Thresholds = ({className}: Props) => {
  const [quantity, selectQuantity] = React.useState<Quantity | undefined>(undefined);
  const [operator, selectOperator] = React.useState<Operator | undefined>(undefined);
  const [meterValue, selectMeterValue] = React.useState('');

  const onChangeQuantity = (event, index, newValue: string) => selectQuantity(newValue as Quantity);
  const onChangeOperator = (event, index, newValue: string) => selectOperator(newValue as Operator);
  const onChangeMeterValue = (event, newValue: string) => selectMeterValue(newValue);

  return (
    <Row className={className}>
      <DropDownMenu
        onChange={onChangeQuantity}
        value={quantity}
        style={quantityDropDownStyle}
      >
        {quantityMenuItems}
      </DropDownMenu>

      <DropDownMenu
        onChange={onChangeOperator}
        value={operator}
        style={operatorDropDownStyle}
      >
        {operatorMenuItems}
      </DropDownMenu>

      <RowMiddle>
        <TextFieldInput
          autoComplete="off"
          hintText={firstUpperTranslated('meter value')}
          value={meterValue}
          onChange={onChangeMeterValue}
          style={textFieldStyle}
        />
        {quantity && <Medium className="Unit">{quantityUnits[Quantity[quantity]]}</Medium>}
      </RowMiddle>
    </Row>
  );
};
