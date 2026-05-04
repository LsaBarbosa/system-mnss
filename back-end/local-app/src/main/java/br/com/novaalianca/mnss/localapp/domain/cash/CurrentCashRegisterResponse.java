package br.com.novaalianca.mnss.localapp.domain.cash;

public record CurrentCashRegisterResponse(boolean open, CashRegisterResponse cashRegister) {
    static CurrentCashRegisterResponse empty() {
        return new CurrentCashRegisterResponse(false, null);
    }

    static CurrentCashRegisterResponse open(CashRegisterEntity cashRegister) {
        return new CurrentCashRegisterResponse(true, CashRegisterResponse.from(cashRegister));
    }
}
