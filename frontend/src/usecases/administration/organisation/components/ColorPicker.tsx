import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {ColorChangeHandler} from 'react-color';
import SketchPicker from 'react-color/lib/components/sketch/Sketch';
import {Color} from '../../../../app/colors';
import {Column} from '../../../../components/layouts/column/Column';
import {RowRight} from '../../../../components/layouts/row/Row';
import {translate} from '../../../../services/translationService';
import {CallbackWith} from '../../../../types/Types';

const presetColors: string[] = [
  '#f44336',
  '#e91e63',
  '#9c27b0',
  '#673ab7',
  '#3f51b5',
  '#2196f3',
  '#03a9f4',
  '#00bcd4',
  '#009688',
  '#4caf50',
  '#8bc34a',
  '#cddc39',
  '#ffeb3b',
  '#ffc107',
  '#ff9800',
  '#ff5722',
  '#795548',
  '#607d8b',
];

interface ColorPickerProps {
  color: Color;
  onChange: CallbackWith<Color>;
}

export const ColorPicker = ({color, onChange}: ColorPickerProps) => {
  const [newColor, changeColor] = React.useState<Color>(color);
  const onChangeHandler: ColorChangeHandler = ({hex}) => changeColor(hex);
  const applyColor = () => onChange(newColor);
  return (
    <Column>
      <SketchPicker
        color={newColor}
        disableAlpha={true}
        onChangeComplete={onChangeHandler}
        presetColors={presetColors}
      />
      <RowRight style={{paddingRight: 12, paddingBottom: 12}}>
        <FlatButton
          disabled={color === newColor}
          primary={true}
          style={{fontWeight: 'bold'}}
          label={translate('apply')}
          onClick={applyColor}
        />
      </RowRight>
    </Column>
  );
};
