import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {TextFieldInput} from '../../../../components/inputs/TextFieldInput';
import {Row, RowMiddle} from '../../../../components/layouts/row/Row';
import {Medium} from '../../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../../services/translationService';
import {
  Quantity,
  quantityAttributes,
  QunantityDisplayMode
} from '../../../../state/ui/graph/measurement/measurementModels';
import {
  OnChangeThreshold,
  RelationalOperator,
  ThresholdQuery
} from '../../../../state/user-selection/userSelectionModels';
import {CallbackWith, ClassNamed, Styled} from '../../../../types/Types';
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

const makeMenuItem = (text) => (
  <MenuItem key={text} primaryText={text} value={text}/>
);

const operatorMenuItems = Object.keys(RelationalOperator)
  .map((key) => RelationalOperator[key])
  .map(makeMenuItem);

const quantityMenuItems = Object.keys(Quantity)
  .sort((a, b) => a.localeCompare(b))
  .map((key) => Quantity[key])
  .map(makeMenuItem);

interface ThresholdProps {
  query?: ThresholdQuery;
  onChange: OnChangeThreshold;
}

type RenderableThresholdQuery = Partial<{
  [key in keyof ThresholdQuery]: ThresholdQuery[key] | undefined | string
}>;

type Props = ThresholdProps & ClassNamed & Styled;

const propertyState = (
  initialQuery: RenderableThresholdQuery,
  onChange: OnChangeThreshold
): [RenderableThresholdQuery, CallbackWith<RenderableThresholdQuery>] => {
  const [value, updateProperty] = React.useState<RenderableThresholdQuery>(initialQuery);
  const fireActionAndUpdateState = (query: RenderableThresholdQuery) => {
    onChange(query as ThresholdQuery);
    updateProperty(query);
  };
  return [value, fireActionAndUpdateState];
};

const emptyQuery: RenderableThresholdQuery = {
  value: '',
  quantity: undefined,
  relationalOperator: undefined,
  unit: undefined,
};

export const Thresholds = (props: Props) => {
  const {query = emptyQuery, onChange, className} = props;
  const [currentQuery, setQuery] = propertyState(query, onChange);
  const {quantity, relationalOperator, value, unit} = currentQuery;

  const onChangeQuantity = (event, index, newValue: string) => setQuery({
    ...currentQuery,
    quantity: newValue as Quantity,
    unit: quantityAttributes[newValue as Quantity].unit
  });

  const onChangeRelationalOperator = (event, index, newValue: string) => setQuery({
    ...currentQuery,
    relationalOperator: newValue as RelationalOperator,
  });

  const onChangeValue = (event, newValue: string) => setQuery({...currentQuery, value: newValue});
  const displayModeText = quantityAttributes[quantity as Quantity].displayMode ===
                     QunantityDisplayMode.consumption ? 'consumption' : 'meter value';
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
        onChange={onChangeRelationalOperator}
        value={relationalOperator}
        style={operatorDropDownStyle}
      >
        {operatorMenuItems}
      </DropDownMenu>

      <RowMiddle>
        <TextFieldInput
          onChange={onChangeValue}
          value={value}
          style={textFieldStyle}
          autoComplete="off"
          hintText={firstUpperTranslated(displayModeText)}
        />
        <Medium className="Unit">{unit}</Medium>
      </RowMiddle>
    </Row>
  );
};
