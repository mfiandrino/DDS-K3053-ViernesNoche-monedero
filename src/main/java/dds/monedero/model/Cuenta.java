package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private Double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0.0;
  }

  public Cuenta(Double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void verificarMontoNegativo(Double monto) {
    if (monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void verificarMaximaCantidadDeDepositos() {
    if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void modificarSaldo(Double monto) {
    this.saldo += monto;
  }

  public void poner(Double cuanto) {
    verificarMontoNegativo(cuanto);
    verificarMaximaCantidadDeDepositos();
    modificarSaldo(cuanto);
    agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, true));
  }

  public void verificarSaldoMenorAlRetiro(Double monto) {
    if (this.saldo - monto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void verificarLimiteDeDineroExtraidoHoy(Double monto) {
    Double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    Double limite = 1000 - montoExtraidoHoy;
    if (monto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }

  public void sacar(Double cuanto) {
    verificarMontoNegativo(cuanto);
    verificarSaldoMenorAlRetiro(cuanto);
    verificarLimiteDeDineroExtraidoHoy(cuanto);
    modificarSaldo(cuanto * -1);
    agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, false));
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public Double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.fueDepositado(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public Double getSaldo() {
    return saldo;
  }

  public void setSaldo(Double saldo) {
    this.saldo = saldo;
  }

}
